/* Licensed under Apache-2.0 2025 */
package com.example.analytics.model;

public record EnrichedPageView(
        String userId, String userName, String userCountry, String pageName, long duration) {}
