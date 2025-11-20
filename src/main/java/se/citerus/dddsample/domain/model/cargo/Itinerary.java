package se.citerus.dddsample.domain.model.cargo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.Validate;
import se.citerus.dddsample.domain.model.handling.HandlingEvent;
import se.citerus.dddsample.domain.model.location.Location;
import se.citerus.dddsample.domain.shared.ValueObject;

/** An itinerary. */
public class Itinerary implements ValueObject<Itinerary> {

  private List<Leg> legs = Collections.emptyList();

  static final Itinerary EMPTY_ITINERARY = new Itinerary();

  private static final Instant END_OF_DAYS = Instant.MAX;

  public Itinerary(final List<Leg> legs) {
    Validate.notEmpty(legs);
    Validate.noNullElements(legs);

    this.legs = legs;
  }

  public List<Leg> legs() {
    return new ArrayList<>(
        legs); // Note: due to JPA requirements, the returned list must be modifiable.
  }

  public boolean isExpected(final HandlingEvent event) {
    if (legs.isEmpty()) {
      return true;
    }

    if (event.type() == HandlingEvent.Type.RECEIVE) {
      // Check that the first leg's origin is the event's location
      final Leg leg = legs.get(0);
      return (leg.loadLocation().equals(event.location()));
    }

    if (event.type() == HandlingEvent.Type.LOAD) {
      // Check that the there is one leg with same load location and voyage
      for (Leg leg : legs) {
        if (leg.loadLocation().sameIdentityAs(event.location())
            && leg.voyage().sameIdentityAs(event.voyage())) return true;
      }
      return false;
    }

    if (event.type() == HandlingEvent.Type.UNLOAD) {
      // Check that the there is one leg with same unload location and voyage
      for (Leg leg : legs) {
        if (leg.unloadLocation().equals(event.location()) && leg.voyage().equals(event.voyage()))
          return true;
      }
      return false;
    }

    if (event.type() == HandlingEvent.Type.CLAIM) {
      // Check that the last leg's destination is from the event's location
      final Leg leg = lastLeg();
      return (leg.unloadLocation().equals(event.location()));
    }

    // HandlingEvent.Type.CUSTOMS;
    return true;
  }

  Location initialDepartureLocation() {
    if (legs.isEmpty()) {
      return Location.UNKNOWN;
    } else {
      return legs.get(0).loadLocation();
    }
  }

  Location finalArrivalLocation() {
    if (legs.isEmpty()) {
      return Location.UNKNOWN;
    } else {
      return lastLeg().unloadLocation();
    }
  }

  Instant finalArrivalDate() {
    final Leg lastLeg = lastLeg();

    if (lastLeg == null) {
      return END_OF_DAYS;
    } else {
      return lastLeg.unloadTime();
    }
  }

  Leg lastLeg() {
    if (legs.isEmpty()) {
      return null;
    } else {
      return legs.get(legs.size() - 1);
    }
  }

  @Override
  public boolean sameValueAs(final Itinerary other) {
    return other != null && legs.equals(other.legs);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final Itinerary itinerary = (Itinerary) o;

    return sameValueAs(itinerary);
  }

  @Override
  public int hashCode() {
    return legs.hashCode();
  }

  Itinerary() {
    // Needed by Hibernate
  }
}
