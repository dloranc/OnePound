package loranc.dawid.onepound;

import loranc.dawid.onepound.Currency.Currency;
import loranc.dawid.onepound.Currency.ICurrencyRateSource;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void checkCurrency() throws Exception {
        ICurrencyRateSource source = mock(ICurrencyRateSource.class);
        when(source.getCurrency()).thenReturn("4.1");

        Currency currency = new Currency(source);

        assertEquals("4.1", currency.getCurrency());
    }
}