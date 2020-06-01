package com.vmware.tsalm.scdf.domain.model.service;

public interface ObservabilityService {
    void send(String metricInWavefrontFormat);
}
