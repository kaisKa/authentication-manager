package ava.io.authentication_manager.feigns;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
@FeignClient(value = "jplaceholder", url = "https://jsonplaceholder.typicode.com/")
public interface FeignExample {
    @RequestMapping(method = RequestMethod.GET, value = "/posts")
    List<String> getPosts();

    @RequestMapping(method = RequestMethod.GET, value = "/posts/{postId}", produces = "application/json")
    String getPostById(@PathVariable("postId") Long postId);
}