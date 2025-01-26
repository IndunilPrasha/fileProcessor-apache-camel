package com.camel.routing.utility;

import com.camel.routing.utility.BatchAggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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

    // Handle null newExchange parameter
    @Test
    public void test_aggregate_handles_null_new_exchange() {
        BatchAggregationStrategy strategy = new BatchAggregationStrategy();

        Exchange oldExchange = mock(Exchange.class);
        Message oldMessage = mock(Message.class);
        List<List<String>> existingBatch = new ArrayList<>();

        when(oldExchange.getIn()).thenReturn(oldMessage);
        when(oldMessage.getBody(List.class)).thenReturn(existingBatch);

        assertThrows(NullPointerException.class, () -> {
            strategy.aggregate(oldExchange, null);
        });
    }

}
