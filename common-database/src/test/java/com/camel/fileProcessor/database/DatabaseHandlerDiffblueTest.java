package com.camel.fileProcessor.database;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.camel.fileProcessor.exception.GlobalExceptionHandler;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {DatabaseHandler.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class DatabaseHandlerDiffblueTest {
    @Autowired
    private DatabaseHandler databaseHandler;

    @MockBean
    private GlobalExceptionHandler globalExceptionHandler;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    /**
     * Test {@link DatabaseHandler#configure()}.
     * <p>
     * Method under test: {@link DatabaseHandler#configure()}
     */
    @Test
    @DisplayName("Test configure()")
    void testConfigure() throws Exception {
        // TODO: Diffblue Cover was only able to create a partial test for this method:
        //   Diffblue AI was unable to find a test

        // Arrange and Act
        databaseHandler.configure();
    }

    /**
     * Test {@link DatabaseHandler#validateInput(Exchange)}.
     * <ul>
     *   <li>When {@link DefaultCamelContext#DefaultCamelContext(boolean)} with init
     * is {@code true}.</li>
     * </ul>
     * <p>
     * Method under test: {@link DatabaseHandler#validateInput(Exchange)}
     */
    @Test
    @DisplayName("Test validateInput(Exchange); when DefaultCamelContext(boolean) with init is 'true'")
    void testValidateInput_whenDefaultCamelContextWithInitIsTrue() throws IllegalArgumentException {
        // Arrange, Act and Assert
        assertThrows(IllegalArgumentException.class,
                () -> databaseHandler.validateInput(new DefaultExchange(new DefaultCamelContext(true))));
    }

    /**
     * Test {@link DatabaseHandler#validateInput(Exchange)}.
     * <ul>
     *   <li>When {@link DefaultExchange#DefaultExchange(CamelContext)} with context
     * is {@link DefaultCamelContext#DefaultCamelContext()}.</li>
     * </ul>
     * <p>
     * Method under test: {@link DatabaseHandler#validateInput(Exchange)}
     */
    @Test
    @DisplayName("Test validateInput(Exchange); when DefaultExchange(CamelContext) with context is DefaultCamelContext()")
    void testValidateInput_whenDefaultExchangeWithContextIsDefaultCamelContext() throws IllegalArgumentException {
        // Arrange, Act and Assert
        assertThrows(IllegalArgumentException.class,
                () -> databaseHandler.validateInput(new DefaultExchange(new DefaultCamelContext())));
    }
}
