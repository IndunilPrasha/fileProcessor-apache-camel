//package com.camel.service;
//
//import com.camel.routing.FileProcessingRoute;
//import org.apache.camel.CamelContext;
//import org.apache.camel.builder.AdviceWithRouteBuilder;
//import org.apache.camel.impl.DefaultCamelContext;
//import org.apache.camel.model.ModelCamelContext;
//import org.apache.camel.test.junit5.CamelTestSupport;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//public class FileProcessingRouteTest extends CamelTestSupport {
//
//    private FileProcessingRoute fileProcessingRoute;
//
//    @BeforeEach
//    public void setUp() {
//        fileProcessingRoute = new FileProcessingRoute(null, null); // Mock dependencies if needed
//    }
//
//    @Test
//    void testFileProcessingRoute() throws Exception {
//        // Use ModelCamelContext to access route definitions
//        ModelCamelContext modelCamelContext = context.adapt(ModelCamelContext.class);
//        modelCamelContext.addRoutes(fileProcessingRoute);
//
//        modelCamelContext.getRouteDefinition("FileProcessingRoute")
//                .adviceWith(modelCamelContext, new AdviceWithRouteBuilder() {
//                    @Override
//                    public void configure() throws Exception {
//                        replaceFromWith("direct:start");
//                        mockEndpointsAndSkip("direct:DatabaseHandler");
//                    }
//                });
//
//        context.start();
//
//        template.sendBodyAndHeader("direct:start", "Test CSV Content", "CamelFileName", "test-file.csv");
//
//        getMockEndpoint("mock:direct:DatabaseHandler").expectedMessageCount(1);
//        assertMockEndpointsSatisfied();
//    }
//}
