package com.camel.routing;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class BatchAggregationStrategyTest {

    // Aggregate first exchange when oldExchange is null
    @Test
    public void test_aggregate_first_exchange_when_old_exchange_null() {
        BatchAggregationStrategy strategy = new BatchAggregationStrategy();

        Exchange newExchange = mock(Exchange.class);
        Message message = mock(Message.class);
        List<String> newRow = Arrays.asList("test1", "test2");

        when(newExchange.getIn()).thenReturn(message);
        when(message.getBody(List.class)).thenReturn(newRow);

        Exchange result = strategy.aggregate(null, newExchange);

        assertEquals(newExchange, result);
        verify(message).setBody(argThat(batch -> {
            List<List<String>> batchList = (List<List<String>>) batch;
            return batchList.size() == 1 && batchList.get(0).equals(newRow);
        }));
    }

}
