package io.pactflow.example.kafka;

import au.com.dius.pact.core.model.Interaction;
import au.com.dius.pact.core.model.Pact;
import au.com.dius.pact.provider.MessageAndMetadata;
import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.AmpqTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;

import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;

@Provider("pactflow-example-provider-java-kafka")
@PactBroker(scheme = "https", host = "tekadvisory.pactflow.io",
        consumerVersionSelectors = {@VersionSelector(tag = "master"), @VersionSelector(tag = "prod")},
        authentication = @PactBrokerAuth(token = "1PDJzb4GL6SNU0_YT4-rXg"))
  public class ProductsKafkaProducerTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProductsKafkaProducerTest.class);

  @TestTemplate
  @ExtendWith(PactVerificationInvocationContextProvider.class)
  void testTemplate(Pact pact, Interaction interaction, PactVerificationContext context) {
    context.verifyInteraction();
  }

  @BeforeEach
  void before(PactVerificationContext context) {
    context.setTarget(new AmpqTestTarget());

    System.out.println("GIT_COMMIT" + System.getenv("GIT_COMMIT"));
    System.setProperty("pact.provider.version",
        System.getenv("GIT_COMMIT") == null ? "" : System.getenv("GIT_COMMIT"));
    System.setProperty("pact.provider.tag",
        System.getenv("GIT_BRANCH") == null ? "" : System.getenv("GIT_BRANCH"));
    System.setProperty("pact.verifier.publishResults",
        System.getenv("PACT_BROKER_PUBLISH_VERIFICATION_RESULTS") == null ? "false" : "true");
  }

  @PactVerifyProvider("a product created event")
  public MessageAndMetadata productCreatedEvent() throws JsonProcessingException {
    ProductEvent product = new ProductEvent("id1", "product name", "type", "v1", EventType.CREATED, 27.00, "optParamValue");
    Message<String> message = new ProductMessageBuilder().withProduct(product).build();

    return generateMessageAndMetadata(message);
  }

  @PactVerifyProvider("a product created event with optional field")
  public MessageAndMetadata productCreatedEventWithOptParam() throws JsonProcessingException {
    ProductEvent product = new ProductEvent("id1", "product name", "type", "v1", EventType.CREATED, 27.00, null);
    Message<String> message = new ProductMessageBuilder().withProduct(product).build();

    return generateMessageAndMetadata(message);
  }

  private MessageAndMetadata generateMessageAndMetadata(Message<String> message) {
    HashMap<String, Object> metadata = new HashMap<String, Object>();
    message.getHeaders().forEach((k, v) -> metadata.put(k, v));

    return new MessageAndMetadata(message.getPayload().getBytes(), metadata);
  }
}
