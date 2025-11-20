package se.citerus.dddsample.domain.model.cargo;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public class TrackingIdTest {

  @Test
  public void testConstructor() {
    assertThatThrownBy(() -> new TrackingId(null)).isInstanceOf(NullPointerException.class);
  }
}
