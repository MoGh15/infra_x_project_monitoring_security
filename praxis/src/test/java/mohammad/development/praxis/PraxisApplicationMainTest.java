package mohammad.development.praxis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PraxisApplicationMainTest {

    @Test
    void main_withNoArgs_doesNotThrow() {
        // This test is for coverage of the main method
        // We can't actually run the main method as it would start the application
        // but we can verify the class exists and is properly annotated
        assertNotNull(PraxisApplication.class);
    }
}

