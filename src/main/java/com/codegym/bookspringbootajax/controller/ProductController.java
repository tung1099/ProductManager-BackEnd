package com.codegym.bookspringbootajax.controller;

import com.codegym.bookspringbootajax.model.Product;
import com.codegym.bookspringbootajax.model.ProductForm;
import com.codegym.bookspringbootajax.service.IProductService;
import com.codegym.bookspringbootajax.service.ProductService;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/products")
@CrossOrigin("*")
public class ProductController {
    @Autowired
    private IProductService productService = new ProductService();

    @Autowired
    Environment env;

    @GetMapping
    public ResponseEntity<Iterable<Product>> showAll(){
        return new ResponseEntity<>(productService.findAll(), HttpStatus.OK);
    }
    @PostMapping
    private ResponseEntity<Product> saveProduct(@ModelAttribute ProductForm productForm){
        MultipartFile multipartFile = productForm.getImage();
        String fileName = multipartFile.getOriginalFilename();
        String fileUpload = env.getProperty("upload.path");
        try {
            FileCopyUtils.copy(multipartFile.getBytes(),new File(fileUpload + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Product product = new Product(productForm.getName(),productForm.getPrice(),productForm.getQuantity(),productForm.getDescription(),fileName);
        productService.save(product);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Product> deleteProduct(@PathVariable Long id){
        Optional<Product> optionalProduct = productService.findById(id);
        if (!optionalProduct.isPresent()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        else {
            productService.remove(id);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }
    }
    @PostMapping("/edit/{id}")
    public ResponseEntity<Product> editProduct(@PathVariable Long id, @ModelAttribute ProductForm productForm){
        Optional<Product> productOptional = productService.findById(id);
        productForm.setId(productOptional.get().getId());
        MultipartFile multipartFile = productForm.getImage();
        String fileName = multipartFile.getOriginalFilename();
        String fileUpload = env.getProperty("upload.path");
        Product existProduct = new Product(id,productForm.getName(),productForm.getPrice(),productForm.getQuantity(), productForm.getDescription(), fileName);
        try {
            FileCopyUtils.copy(multipartFile.getBytes(),new File(fileUpload + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        if (existProduct.getImage().equals("filename.jpg")) {
//            existProduct.setImage(productOptional.get().getImage());
//        }
        productService.save(existProduct);
        return new ResponseEntity<>(existProduct,HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Product> findOne(@PathVariable Long id){
        return new ResponseEntity<>(productService.findById(id).get(),HttpStatus.OK);
    }
}