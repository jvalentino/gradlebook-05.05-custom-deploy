package com.blogspot.jvalentino.gradle.service

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session

/**
 * <p>General service for SSH/SCP operations</p>
 * @author jvalentino2
 */
@SuppressWarnings(['Println'])
class RemoteService {

    Session generateSession(String username, String password,
            String host) {
        JSch jsch = new JSch()
        Session session = jsch.getSession(
                username, host, 22)
        session.password = password
        session.setConfig('StrictHostKeyChecking', 'no')
        session
    }

    String execute(String command, Session session) {
        ChannelExec channelExec = session.openChannel('exec')

        InputStream is = channelExec.inputStream

        println "  \$ ${command}"
        channelExec.command = command
        channelExec.connect()

        String text = is.text.trim()
        if (text != null && text != '') {
            println "  > ${text}"
        }

        channelExec.disconnect()

        text
    }

    void upload(File file, String toRemoteFile, Session session) {
        ChannelSftp sftp = session.openChannel('sftp')
        sftp.connect()
        file.withInputStream { 
            istream -> sftp.put(istream, toRemoteFile) 
        }
        sftp.disconnect()
    }
}
