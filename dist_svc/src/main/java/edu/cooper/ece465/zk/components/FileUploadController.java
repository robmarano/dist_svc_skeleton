package edu.cooper.ece465.zk.components;

import java.io.IOException;
import java.util.stream.Collectors;

import edu.cooper.ece465.zk.api.ZkService;
import edu.cooper.ece465.zk.impl.ZkServiceImpl;
import edu.cooper.ece465.zk.services.StorageFileNotFoundException;
import edu.cooper.ece465.zk.services.StorageService;
import edu.cooper.ece465.zk.util.ClusterInfo;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static edu.cooper.ece465.zk.util.ZkDemoUtil.ALL_NODES;
import static edu.cooper.ece465.zk.util.ZkDemoUtil.APP;

@Controller
public class FileUploadController {

    private final StorageService storageService;
    @Autowired
    private ZkService zkService;


    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/") // the index of the site/service
    public String listUploadedFiles(Model model) throws IOException {

        //List<String> liveNodes =

        model.addAttribute("files",
                storageService.loadAll().map(
                        path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                                "serveFile", path.getFileName().toString()).build().toUri().toString()
                ).collect(Collectors.toList()));

        System.out.printf("listUploadedFiles: %s\n", model.toString());
        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);

        if (file == null)
            return ResponseEntity.notFound().build();

        String remoteAddress = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getRemoteAddr();

        System.out.printf("Sending file %s to requester %s\n",filename, remoteAddress);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        String remoteAddress = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getRemoteAddr();
        try {
            zkService.createNodeInAppZnode(remoteAddress, remoteAddress);
        } catch (KeeperException.NodeExistsException ex) {
            System.out.printf("Node %s already exists\n", remoteAddress);
        }

//        sb.delete(0, sb.length());
        StringBuilder sb = new StringBuilder(remoteAddress);
        sb.append("/");
        sb.append(file.getOriginalFilename());
        try {
            zkService.createNodeInAppZnode(sb.toString(), ClusterInfo.getClusterInfo().getMaster());
        } catch (KeeperException.NodeExistsException ex) {
            System.out.printf("Node %s already exists\n", sb.toString());
        }

        storageService.store(file);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
