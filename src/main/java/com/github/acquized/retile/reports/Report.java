/* Copyright 2016 Acquized
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.acquized.retile.reports;

import com.github.acquized.retile.annotations.Documented;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a unique Report made by a User regarding
 * any other User. The Class consists of following
 * variables:
 *
 * <ul>
 *     <li>{@link com.github.acquized.retile.reports.Report#token} - Represents the Unique ID Token for a Report
 *     (a report can have multiple {@link com.github.acquized.retile.reports.Report} Objects assigned to it
 *     <li>{@link com.github.acquized.retile.reports.Report#reporter} - Represents the Player that reported the {@code victim}
 *     <li>{@link com.github.acquized.retile.reports.Report#victim} - Represents the Player that got reported by the {@code reporter}
 *     <li>{@link com.github.acquized.retile.reports.Report#reason} - Represents the Reason why the {@code victim} got reported by the {@code reporter}
 *     <li>{@link com.github.acquized.retile.reports.Report#timestamp} - Represents the Time and Date where the Player got reported. This is in milliseconds.
 * </ul>
 *
 * @author Acquized
 */
@Data
@Builder
@Documented
@AllArgsConstructor
public class Report {

    private String token;
    private UUID reporter;
    private UUID victim;
    private String reason;
    private long timestamp;

    public Report(UUID reporter, UUID victim, String reason, long timestamp) {
        this.token = TokenGenerator.generate();
        this.reporter = reporter;
        this.victim = victim;
        this.reason = reason;
        this.timestamp = timestamp;
    }

}
