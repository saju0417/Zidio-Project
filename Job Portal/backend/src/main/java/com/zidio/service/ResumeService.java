package com.zidio.service;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResumeService {
    private final Tika tika = new Tika();
    private final Path uploadDir = Paths.get("uploads");
    public ResumeService() throws IOException { if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir); }
    public String saveAndExtractText(MultipartFile file, String filename) throws IOException {
        Path dest = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        String text = tika.parseToString(dest.toFile());
        return text;
    }
    public double calculateSimilarity(String text1, String text2) {
        Map<String, Integer> freq1 = termFrequency(text1);
        Map<String, Integer> freq2 = termFrequency(text2);
        Set<String> terms = new HashSet<>(); terms.addAll(freq1.keySet()); terms.addAll(freq2.keySet());
        List<Integer> v1 = terms.stream().map(t -> freq1.getOrDefault(t,0)).collect(Collectors.toList());
        List<Integer> v2 = terms.stream().map(t -> freq2.getOrDefault(t,0)).collect(Collectors.toList());
        double dot=0, n1=0, n2=0;
        for (int i=0;i<v1.size();i++){ dot+=v1.get(i)*v2.get(i); n1+=v1.get(i)*v1.get(i); n2+=v2.get(i)*v2.get(i); }
        if (n1==0 || n2==0) return 0.0;
        return dot/(Math.sqrt(n1)*Math.sqrt(n2));
    }
    private Map<String,Integer> termFrequency(String text){
        if (text==null) return Collections.emptyMap();
        String cleaned = text.replaceAll("[^a-zA-Z0-9 ]"," ").toLowerCase();
        String[] tokens = cleaned.split("\s+");
        Map<String,Integer> freq = new HashMap<>();
        for (String t: tokens){ if (t.length()<2) continue; freq.put(t, freq.getOrDefault(t,0)+1); }
        return freq;
    }
    public Path getUploadDir(){ return uploadDir; }
}
