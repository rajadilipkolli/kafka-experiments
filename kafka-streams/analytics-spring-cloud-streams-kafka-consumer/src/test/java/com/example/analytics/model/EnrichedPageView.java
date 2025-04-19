/* Licensed under Apache-2.0 2025 */
package com.example.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EnrichedPageView {
    private String userId;
    private String userName;
    private String userCountry;
    private String pageName;
    private long duration;

    public EnrichedPageView() {}
}
