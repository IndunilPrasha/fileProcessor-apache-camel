package com.camel.routing.utility;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

import java.util.ArrayList;
import java.util.List;

public class BatchAggregationStrategy implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        List<List<String>> batch;

        if (oldExchange == null) {
            batch = new ArrayList<>();
            oldExchange = newExchange;
        } else {
            batch = oldExchange.getIn().getBody(List.class);
        }

        List<String> newRow = newExchange.getIn().getBody(List.class);
        batch.add(newRow);
        oldExchange.getIn().setBody(batch);

        return oldExchange;
    }
}
