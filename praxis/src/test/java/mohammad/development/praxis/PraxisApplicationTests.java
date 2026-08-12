package mohammad.development.praxis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PraxisApplicationTests {

	@Test
	void main_class_exists() {
		assertNotNull(PraxisApplication.class);
	}

	@Test
	void main_method_accessible() {
		assertDoesNotThrow(() -> {
			// Test that main class is properly configured
			Class<?> clazz = PraxisApplication.class;
			assertNotNull(clazz.getMethod("main", String[].class));
		});
	}
}
