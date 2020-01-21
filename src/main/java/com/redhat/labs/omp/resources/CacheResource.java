package com.redhat.labs.omp.resources;

import com.redhat.labs.cache.cacheStore.ResidencyDataCache;
import com.redhat.labs.omp.models.GetFileResponse;
import com.redhat.labs.omp.models.filesmanagement.GetMultipleFilesResponse;
import com.redhat.labs.omp.models.filesmanagement.SingleFileResponse;
import com.redhat.labs.omp.resources.filters.Logged;
import com.redhat.labs.omp.services.GitLabService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Path("/api/cache")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Liveness
@Readiness
public class CacheResource {

    @ConfigProperty(name = "templateRepositoryId", defaultValue = "9407")
    private String templateRepositoryId;


    @ConfigProperty(name = "configFileFolder", defaultValue = "schema")
    private String configFileFolder;

    @Inject
    @RestClient
    public GitLabService gitLabService;


    public static Logger logger = LoggerFactory.getLogger(CacheResource.class);


    public CacheResource(){
        residencyDataCacheForConfig = new ResidencyDataCache();
    }

    private final ResidencyDataCache residencyDataCacheForConfig;

    private static final String CONFIG_FILE_CACHE_KEY = "configFile";

    /**
     * This will trigger a fetch from Git/config.yaml and store it in cache
     *
     * @return
     */
    @POST
    @Logged
    public void updateConfigFromCache() {

        SingleFileResponse configFileContent = fetchContentFromGit(configFileFolder + "/config.yaml");

        residencyDataCacheForConfig.store(CONFIG_FILE_CACHE_KEY, configFileContent.getFileContent());
    }



    private SingleFileResponse fetchContentFromGit(String fileName) {
        GetFileResponse metaFileResponse = gitLabService.getFile(templateRepositoryId, fileName, "master");
        String base64Content = metaFileResponse.content;
        String content = new String(Base64.getDecoder().decode(base64Content), StandardCharsets.UTF_8);
        logger.info("File {} content fetched {}", fileName, content);
        return new SingleFileResponse(fileName, content);
    }
}
