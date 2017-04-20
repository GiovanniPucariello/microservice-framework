package uk.gov.justice.services.adapter.direct;

import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.DirectAdapter;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@DirectAdapter(
    component = QUERY_VIEW,
    actions = {"people.search-users"}
)
public class QueryViewAdapter implements SynchronousDirectAdapter {
  @Inject
  InterceptorChainProcessor interceptorChainProcessor;

  DirectAdapterProcessor directAdapterProcessor = new DefaultDirectAdapterProcessor();

  @Override
  public JsonEnvelope process(JsonEnvelope envelope) {
    return directAdapterProcessor.process(envelope, interceptorChainProcessor::process);
  }
}
