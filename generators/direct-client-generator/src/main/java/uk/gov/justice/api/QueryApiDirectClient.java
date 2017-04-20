package uk.gov.justice.api;


import uk.gov.justice.services.adapter.direct.QueryViewAdapter;
import uk.gov.justice.services.core.annotation.Direct;
import uk.gov.justice.services.core.annotation.FrameworkComponent;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@Direct
@FrameworkComponent("QUERY_API")
public class QueryApiDirectClient {

    @Inject
    QueryViewAdapter adapter;

    @Handles("people.get-user1")
    public JsonEnvelope getUsersUserIdPeopleQueryUser1(final JsonEnvelope envelope) {
        return adapter.process(envelope);
    }

    @Handles("people.get-user2")
    public JsonEnvelope getUsersUserIdPeopleQueryUser2(final JsonEnvelope envelope) {
        return adapter.process(envelope);
    }

}
