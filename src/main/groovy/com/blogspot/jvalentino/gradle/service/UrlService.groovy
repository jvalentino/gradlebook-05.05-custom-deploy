package com.blogspot.jvalentino.gradle.service

import com.blogspot.jvalentino.gradle.ext.Health

import groovy.json.JsonSlurper

/**
 * <p>Service for HTTP operations</p>
 * @author jvalentino2
 */
@SuppressWarnings(['Println'])
class UrlService {

    UrlService instance = this
    
    String post(String urlString) {
        String text = null
        println "  \$ POST ${urlString}"
        URL baseUrl = new URL(urlString)
        String queryString = ''
        HttpURLConnection connection = baseUrl.openConnection()
        try {
            connection.with {
                doOutput = true
                requestMethod = 'POST'
                outputStream.withWriter { 
                    writer -> writer << queryString }
                text = content.text?.trim()
                println "  > ${text}"
            }
        } catch (e) {
            println "  > ${e.message}"
        }
        text
    }

    Health getHealth(String url) {
        Health result = Health.NOT_RUNNING
        try {
            println "  \$ GET ${url}"
            String jsonText = new URL(url).text
            println "  > ${jsonText}"
            Map json = new JsonSlurper().parseText(jsonText)
            result = json.status == 'UP' ? Health.UP : Health.DOWN
        } catch (e) {
            println "  > ${e.message}"
        }

        result
    }

    void waitUntilHealth(Health starting, Health ending, long timeout,
            long sleepInterval, String url) throws TimeoutException {
        Health health = starting

        long currentWaitTime = 0

        while (health != ending) {
            println '- Checking health status...'
            health = instance.getHealth(url)
            println "- Result is Health.${health}"
            if (health != ending) {
                if (currentWaitTime > timeout) {
                    throw new TimeoutException("The desired response " + 
                        "could not be reached in ${timeout} ms")
                }
                currentWaitTime += sleepInterval
                instance.doSleep(sleepInterval)
            }
        }
    }
    
    void doSleep(long time) {
        sleep(time)
    }
}
