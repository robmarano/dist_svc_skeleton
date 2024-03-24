package edu.cooper.ece465.zk.components;

import edu.cooper.ece465.zk.api.ZkService;
import edu.cooper.ece465.zk.impl.ZkServiceImpl;
import edu.cooper.ece465.zk.model.File;
import edu.cooper.ece465.zk.util.ClusterInfo;
import edu.cooper.ece465.zk.util.DataStorage;
import java.util.List;

import edu.cooper.ece465.zk.util.OnStartUpApplication;
import edu.cooper.ece465.zk.util.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import static edu.cooper.ece465.zk.util.ZkDemoUtil.*;

/** @author "Bikas Katwal" 26/03/19 */
@RestController
public class ZookeeperDemoController {
    @Autowired
    private ZkService zkService;

    private RestTemplate restTemplate = new RestTemplate();

    /*
     * example post using CURL
     * curl -X POST -H "Content-Type: application/json" -d '{ "name": "John Doe", "age": 30 }' https://example.com/api/v1/users
     */
    @PutMapping("/file/{id}/{directory}/{name}")
    public ResponseEntity<String> savePerson(
            HttpServletRequest request,
            @PathVariable("id") Integer id,
            @PathVariable("directory") String directory,
            @PathVariable("name") String name) {

        System.out.println(request);
        String requestFrom = request.getHeader("request_from");
        String leader = ClusterInfo.getClusterInfo().getMaster();

        // TODO
        String randomString = Utils.generateRandomString(16);
        zkService.setZNodeData(DATA, randomString);

        if (!isEmpty(requestFrom) && requestFrom.equalsIgnoreCase(leader)) {
            File file = new File(id, directory, name);
            DataStorage.setFile(file);
            return ResponseEntity.ok("SUCCESS");
        }
        // If I am leader I will broadcast data to all live node, else forward request to leader
        if (amILeader()) {
            List<String> liveNodes = ClusterInfo.getClusterInfo().getLiveNodes();

            int successCount = 0;
            for (String node : liveNodes) {

                if (getHostPortOfServer().equals(node)) {
                    File file = new File(id, directory, name);
                    DataStorage.setFile(file);
                    successCount++;
                } else {
                    String requestUrl =
                            "http://"
                                    .concat(node)
                                    .concat("/")
                                    .concat("file")
                                    .concat("/")
                                    .concat(String.valueOf(id))
                                    .concat("/")
                                    .concat(name);
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("request_from", leader);
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<String> entity = new HttpEntity<>(headers);
                    restTemplate.exchange(requestUrl, HttpMethod.PUT, entity, String.class).getBody();
                    successCount++;
                }
            }

            return ResponseEntity.ok()
                    .body("Successfully update ".concat(String.valueOf(successCount)).concat(" nodes"));
        } else {
            String requestUrl =
                    "http://"
                            .concat(leader)
                            .concat("/")
                            .concat("file")
                            .concat("/")
                            .concat(String.valueOf(id))
                            .concat("/")
                            .concat(directory)
                            .concat("/")
                            .concat(name);
            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(requestUrl, HttpMethod.PUT, entity, String.class);
        }
    }

    private boolean amILeader() {
        String leader = ClusterInfo.getClusterInfo().getMaster();
        return getHostPortOfServer().equals(leader);
    }

    @GetMapping("/files")
    public ResponseEntity<List<File>> getPerson() {

        return ResponseEntity.ok(DataStorage.getFileListFromStorage());
    }

    @GetMapping("/clusterInfo")
    public ResponseEntity<ClusterInfo> getClusterinfo() {

        return ResponseEntity.ok(ClusterInfo.getClusterInfo());
    }
}