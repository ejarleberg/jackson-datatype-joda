package com.fasterxml.jackson.datatype.joda.deser;

import java.io.IOException;

import org.joda.time.*;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

public class PeriodDeserializer
    extends JodaDeserializerBase<ReadablePeriod>
{
    private static final long serialVersionUID = 1L;

    private final static PeriodFormatter DEFAULT_FORMAT = ISOPeriodFormat.standard();
    
    private final boolean _requireFullPeriod;
    
    public PeriodDeserializer() {
        this(true);
    }
    
    public PeriodDeserializer(boolean fullPeriod) {
        super(fullPeriod ? Period.class : ReadablePeriod.class);
        _requireFullPeriod = fullPeriod;
    }
   
    @Override
    public ReadablePeriod deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException
    {
        JsonToken t = p.getCurrentToken();
        if (t == JsonToken.VALUE_STRING) {
            String str = p.getText().trim();
            if (str.isEmpty()) {
                return null;
            }
            return DEFAULT_FORMAT.parsePeriod(str);
        }
        if (t == JsonToken.VALUE_NUMBER_INT) {
            return new Period(p.getLongValue());    
        }
        if (t != JsonToken.START_OBJECT && t != JsonToken.FIELD_NAME) {
            return (ReadablePeriod) ctxt.handleUnexpectedToken(handledType(), t, p,
                    "expected JSON Number, String or Object");
        }
        
        JsonNode treeNode = p.readValueAsTree();
        String periodType = treeNode.path("fieldType").path("name").asText();
        String periodName = treeNode.path("periodType").path("name").asText();
        // any "weird" numbers we should worry about?
        int periodValue = treeNode.path(periodType).asInt();

        ReadablePeriod rp;
        
        if (periodName.equals( "Seconds" )) {
            rp = Seconds.seconds( periodValue );
        }
        else if (periodName.equals( "Minutes" )) {
            rp = Minutes.minutes( periodValue );
        }
        else if (periodName.equals( "Hours" )) {
            rp = Hours.hours( periodValue );
        }
        else if (periodName.equals( "Days" )) {
            rp = Days.days( periodValue );
        }
        else if (periodName.equals( "Weeks" )) {
            rp = Weeks.weeks( periodValue );
        }
        else if (periodName.equals( "Months" )) {
            rp = Months.months( periodValue );
        }
        else if (periodName.equals( "Years" )) {
            rp = Years.years( periodValue );
        } else {
            ctxt.reportInputMismatch(handledType(),
                    "Don't know how to deserialize %s using periodName '%s'",
                    handledType().getName(), periodName);
            rp = null; // never gets here
        }

        if (_requireFullPeriod && !(rp instanceof Period)) {
            rp = rp.toPeriod();
        }
        return rp;
    }
}
