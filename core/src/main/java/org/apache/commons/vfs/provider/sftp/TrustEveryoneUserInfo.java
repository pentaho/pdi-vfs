/*!
* Copyright 2010 - 2013 Pentaho Corporation.  All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/

package org.apache.commons.vfs.provider.sftp;

import com.jcraft.jsch.UserInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Helper class to trust a new host
 */
public class TrustEveryoneUserInfo implements UserInfo
{
    private Log log = LogFactory.getLog(TrustEveryoneUserInfo.class);

    public String getPassphrase()
    {
        return null;
    }

    public String getPassword()
    {
        return null;
    }

    public boolean promptPassword(String s)
    {
        log.info(s + " - Answer: False");
        return false;
    }

    public boolean promptPassphrase(String s)
    {
        log.info(s + " - Answer: False");
        return false;
    }

    public boolean promptYesNo(String s)
    {
        log.debug(s + " - Answer: Yes");

        // trust
        return true;
    }

    public void showMessage(String s)
    {
        log.debug(s);
    }
}
