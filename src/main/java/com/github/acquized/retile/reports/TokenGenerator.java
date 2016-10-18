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

import com.google.common.io.BaseEncoding;

import com.github.acquized.retile.annotations.Beta;
import com.github.acquized.retile.annotations.Documented;

import java.security.SecureRandom;

@Beta
@Documented
public class TokenGenerator {

    private static SecureRandom random = new SecureRandom();

    /**
     * Generates a random Byte Array which will be filled
     * using a SecureRandom. This random Byte Array will
     * be encoded into a Base64 String which will be
     * crippled down to a 12 character String. This will
     * then be used for the UniqueId of a Report.
     *
     * @author Acquized
     * @return randomly generated Base64 String with 12 characters
     */
    public static String generate() {
        byte[] array = new byte[9];
        random.nextBytes(array);
        return BaseEncoding.base64().omitPadding().encode(array).replace('/', '-').substring(0, 12); /* Just for safety */
    }

}
