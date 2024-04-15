package com.codewitharjun.fullstackbackend.controller;

import com.codewitharjun.fullstackbackend.exception.ProductNotFoundException;
import com.codewitharjun.fullstackbackend.model.Product;
import com.codewitharjun.fullstackbackend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/products")
@CrossOrigin("*")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    // Upload directory for storing images
    private static final String UPLOAD_DIR = "./uploads/";

    @PostMapping
    public Product createProduct(@RequestParam("image") MultipartFile file, @RequestParam("title") String title,
                                 @RequestParam("description") String description, @RequestParam("price") double price) throws IOException {
        // Save the image to the server
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs(); // Create directory if it doesn't exist
        }
        if (!file.isEmpty()) {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOAD_DIR + file.getOriginalFilename());
            Files.write(path, bytes);
        }

        // Create a new product object
        Product product = new Product();
        product.setTitle(title);
        product.setDescription(description);
        product.setPrice(price);
        product.setImageUrl("/uploads/" + file.getOriginalFilename()); // Set the image URL

        // Save the product to the database
        return productRepository.save(product);
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @PutMapping("/{id}")
    public Product updateProduct(@RequestParam("image") MultipartFile file, @PathVariable Long id, @RequestParam("title") String title,
                                 @RequestParam("description") String description, @RequestParam("price") double price) throws IOException {
        // Save the image to the server
        if (!file.isEmpty()) {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOAD_DIR + file.getOriginalFilename());
            Files.write(path, bytes);
        }

        // Update the product
        return productRepository.findById(id)
                .map(product -> {
                    product.setImageUrl("/uploads/" + file.getOriginalFilename());
                    product.setTitle(title);
                    product.setDescription(description);
                    product.setPrice(price);
                    return productRepository.save(product);
                })
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
        return "Product with id " + id + " has been deleted successfully.";
    }

    // Method to retrieve uploaded files
    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Path file = Paths.get(UPLOAD_DIR).resolve(filename);
        Resource resource;
        try {
            resource = new UrlResource(file.toUri());
        } catch (IOException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }

        // Check if the file exists and is readable
        if (resource.exists() && resource.isReadable()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .contentType(MediaType.IMAGE_JPEG) // Adjust content type based on file type
                    .body(resource);
        } else {
            throw new RuntimeException("File not found or cannot be read: " + filename);
        }
    }

    // Method to retrieve list of uploaded filenames
    @GetMapping("/uploads")
    public List<String> listUploadedFiles() {
        List<String> fileNames = new ArrayList<>();
        File uploadDirectory = new File(UPLOAD_DIR);
        if (uploadDirectory.exists() && uploadDirectory.isDirectory()) {
            File[] files = uploadDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        fileNames.add(file.getName());
                    }
                }
            }
        }
        return fileNames;
    }
}
