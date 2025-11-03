package com.reliablecarriers.Reliable.Carriers.service;

import com.reliablecarriers.Reliable.Carriers.dto.QuoteRequest;
import com.reliablecarriers.Reliable.Carriers.dto.QuoteResponse;

import java.util.Map;

public interface QuoteService {
    QuoteResponse calculateQuote(QuoteRequest request);
    Map<String, Object> getServiceOptions();
    Map<String, Object> validateDimensions(Map<String, Object> dimensions);
}


