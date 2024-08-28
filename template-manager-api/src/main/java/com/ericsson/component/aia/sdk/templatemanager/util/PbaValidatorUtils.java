/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.sdk.templatemanager.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.ericsson.component.aia.sdk.templatemanager.exception.WrongEnvironmentGitUrlException;

/**
 * @author ezsalro
 *
 */
public class PbaValidatorUtils {

    private static final Pattern SCM_PATTERN = Pattern.compile("ssh\\://git@((\\d{1,3}\\.){3,3}\\d{1,3})\\:\\d+.+\\.git");

    private static final Pattern GIT_PATTERN = Pattern.compile("http\\://((\\d{1,3}\\.){3,3}\\d{1,3})/.+");

    private PbaValidatorUtils() {
    }

    /**
     * Validate SCM Tag URL
     *
     * @param scmUrl
     *            - scmUrl
     * @param gitServiceUrl
     *            - original git url
     * @throws WrongEnvironmentGitUrlException
     *             - exception
     */
    public static void validateScmUrl(final String scmUrl, final String gitServiceUrl) throws WrongEnvironmentGitUrlException {
        if (StringUtils.isNotEmpty(scmUrl)) {
            final Matcher matcher = SCM_PATTERN.matcher(scmUrl);
            if (matcher.find()) {
                final String address = matcher.group(1);
                if (gitServiceUrl != null) {
                    if (!gitServiceUrl.contains(address)) {

                        String correctAddress = null;
                        final Matcher match = GIT_PATTERN.matcher(gitServiceUrl);

                        if (match.find()) {
                            correctAddress = match.group(1);
                        }

                        throw new WrongEnvironmentGitUrlException(
                                String.format("Environment git URL is wrong (%s). Correct addres is %s", address, correctAddress));

                    }
                }
            }
        }
    }

}
