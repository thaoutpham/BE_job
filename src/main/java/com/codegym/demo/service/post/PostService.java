package com.codegym.demo.service.post;

import com.codegym.demo.model.Post;
import com.codegym.demo.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class PostService implements IPostService{
    @Autowired
    private PostRepository postRepository;
    @Override
    public Iterable<Post> findAll() {
        return postRepository.findAll();
    }

    @Override
    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    @Override
    public Post save(Post post) {
        return (Post) postRepository.save(post);
    }

    @Override
    public void remove(Long id) {
        postRepository.deleteById(id);
    }


    @Override
    public Iterable<Post> searchAdvance(String title, double salary, String exp, String address) {
        return postRepository.searchAdvanced(title,salary, exp, address);
    }


    @Override
    public Iterable<Post> findTop2New() {
        return postRepository.findTop2New();
    }

    @Override
    public Iterable<Post> findAllByTitleContaining(String title) {
        return postRepository.findAllByTitleContaining(title);
    }


    @Override
    public Iterable<Post> findAllByCompany_Id(Long id) {
        return postRepository.findAllByCompany_Id(id);
    }

    @Override
    public Iterable<Post> findTopSalary() {
        return postRepository.findTopSalary();
    }

}
