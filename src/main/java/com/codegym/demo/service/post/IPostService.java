package com.codegym.demo.service.post;

import com.codegym.demo.model.Post;
import com.codegym.demo.service.IGeneralService;

public interface IPostService extends IGeneralService<Post> {
    Iterable<Post> searchAdvance(String title, double salary,String exp,String address);
    Iterable<Post>findTop2New();
    Iterable<Post> findAllByTitleContaining(String title);
    Iterable<Post> findAllByCompany_Id(Long id);
    Iterable<Post> findTopSalary();
}
