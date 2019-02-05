package com.redbeemedia.enigma.core.exposure.query;

import com.redbeemedia.enigma.core.util.IStringAppendable;
import com.redbeemedia.enigma.core.util.StringAppendable;
import com.redbeemedia.enigma.core.util.UrlPath;

import java.util.ArrayList;
import java.util.Collection;

public class QueryParameterSet implements IQueryParameterSet {
    private Collection<IQueryParameter<?>> queryParameters = new ArrayList<>();

    @Override
    public void add(IQueryParameter<?> queryParameter) {
        this.queryParameters.add(queryParameter);
    }

    @Override
    public UrlPath applyAll(UrlPath urlPath) {
        IStringAppendable query = new StringAppendable();
        query = urlPath.contains("?") ? query.append("&") : query.append("?");

        StringBuilder buffer = new StringBuilder();
        IStringAppendable bufferAppendable = new StringAppendable(buffer);
        boolean first = true;
        for(IQueryParameter<?> queryParameter : queryParameters) {
            buffer.setLength(0);
            queryParameter.apply(bufferAppendable);
            if(buffer.length() > 0) {
                if(first) {
                    first = false;
                } else {
                    query = query.append("&");
                }
                query = query.append(buffer.toString());
            }
        }
        return urlPath.append(query.toString());
    }
}
