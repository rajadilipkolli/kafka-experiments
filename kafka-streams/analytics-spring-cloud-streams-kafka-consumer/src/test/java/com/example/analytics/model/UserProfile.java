/* Licensed under Apache-2.0 2025 */
package com.example.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private String userId;
    private String name;
    private String country;
}
