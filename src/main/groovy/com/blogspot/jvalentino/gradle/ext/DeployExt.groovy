package com.blogspot.jvalentino.gradle.ext

import com.jcraft.jsch.Session

/**
 * <p>Settings for this plugin</p>
 * @author jvalentino2
 */
class DeployExt {
    String config = 'deploy.yml'
    String env
    String username
    String password
    String host
    String targetDir
    Session session
    String deployFile
    String shutdownUrl
    String healthUrl
    String startCommand
}
