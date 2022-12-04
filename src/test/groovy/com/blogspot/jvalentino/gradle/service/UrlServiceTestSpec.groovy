package com.blogspot.jvalentino.gradle.service

import com.blogspot.jvalentino.gradle.ext.Health

import spock.lang.Specification
import spock.lang.Unroll

class UrlServiceTestSpec extends Specification {

    UrlService service
    
    def setup() {
        service = new UrlService()
        service.instance = Mock(UrlService)
        GroovyMock(Thread, gloabl:true)
    }
    
    void "test post"() {
        given:
        String urlString = "http://foo"
        
        and:
        HttpURLConnection conn = Mock(HttpURLConnection)
        URL.class.metaClass.openConnection = {
            conn
        }
        OutputStream os = Mock(OutputStream)
        
        when:
        String text = service.post(urlString)
        
        then:
        1 * conn.setDoOutput(true)
        1 * conn.setRequestMethod('POST')
        1 * conn.getOutputStream() >> os
        1 * conn.content >> ['text':'blah']
        
        and:
        text == 'blah'
    }
    
    @Unroll
    void "Test getHealth for #h"() {
        given:
        String url = 'http://blah'
        
        and:
        URL.class.metaClass.getText = {
            json   
        }
        
        when:
        Health result = service.getHealth(url)
        
        then:
        result == h
        
        where:
        json                    || h
        '{ "status":"UP" }'     || Health.UP
        '{ "status":"DOWN" }'   || Health.DOWN
        '{ "st'                 || Health.NOT_RUNNING
    }
    
    void "test waitUntilHealth"() {
        given:
        Health starting = Health.NOT_RUNNING
        Health ending = Health.UP
        long timeout = 3_000L
        long sleepInterval = 1_000L
        String url = "'http://foo"
        
        when:
        service.waitUntilHealth(
            starting, ending, timeout, sleepInterval, url)
        
        then:
        1 * service.instance.getHealth(url) >> Health.NOT_RUNNING
        1 * service.instance.doSleep(sleepInterval)
        
        and:
        1 * service.instance.getHealth(url) >> Health.UP
    }
}
