package edu.cooper.ece465.zk.services;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
@ConfigurationProperties(prefix = "storage")
@Getter
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    private final String location = "upload-dir";
}