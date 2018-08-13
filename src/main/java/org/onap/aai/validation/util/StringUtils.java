/*
 * ============LICENSE_START===================================================
 * Copyright (c) 2018 Amdocs
 * ============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=====================================================
 */
package org.onap.aai.validation.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.onap.aai.cl.api.Logger;
import org.onap.aai.validation.exception.ValidationServiceError;
import org.onap.aai.validation.exception.ValidationServiceException;
import org.onap.aai.validation.logging.ApplicationMsgs;
import org.onap.aai.validation.logging.LogHelper;

/**
 * Utility class for String manipulation.
 */
public class StringUtils {

    private static final Logger applicationLogger = LogHelper.INSTANCE;

    /**
     * All methods are static.
     */
    private StringUtils() {
        // Do not instantiate
    }

    /**
     * Utility method to strip a prefix or set of prefixes (identified by a delimiter sequence) from the string. This is
     * achieved by finding the index of the last prefix delimiter in the string and removing all characters before and
     * including this index.
     *
     * @param string the String to strip prefixes from
     * @param prefixDelimiter the String that acts as the delimiter for the prefix(es)
     * @return the String minus the prefixes
     */
    public static String stripPrefix(String string, String prefixDelimiter) {
        return string.contains(prefixDelimiter)
                ? string.substring(string.lastIndexOf(prefixDelimiter) + prefixDelimiter.length()) : string;
    }

    /**
     * Strips a prefix identified by a delimiter. This is achieved by splitting the string in two around matches of the
     * first occurrence of the given regular expression.
     *
     * @param string a String from which to strip a prefix
     * @param regex the delimiting regular expression
     * @return
     * @throws ValidationServiceException If there is a problem with the provided regular expression.
     */
    public static String stripPrefixRegex(String string, String regex) throws ValidationServiceException {
        String[] strings = validParameters(string, regex) ? string.split(regex, 2) : new String[0];
        return strings.length == 2 ? strings[1] : string;
    }

    /**
     * Process a list of strings and strip the given suffix from each string in the list.
     *
     * @param stringList a list of strings
     * @param suffix a suffix to be removed from the strings
     * @return stripped list of strings.
     */
    public static List<String> stripSuffix(List<String> stringList, String suffix) {
        List<String> result = new ArrayList<>();
        for (String str : stringList) {
            String stripped = str.endsWith(suffix) ? str.substring(0, str.indexOf(suffix)) : str;
            result.add(stripped);
        }
        return result;
    }

    private static boolean validParameters(String string, String regex) throws ValidationServiceException {
        if (string != null && !string.isEmpty() && regex != null && !regex.isEmpty()) {
            try {
                Pattern.compile(regex);
                return true;
            } catch (PatternSyntaxException e) {
                applicationLogger.error(ApplicationMsgs.STRING_UTILS_INVALID_REGEX, regex);
                throw new ValidationServiceException(ValidationServiceError.STRING_UTILS_INVALID_REGEX, e, regex);
            }
        }
        return false;
    }
}
