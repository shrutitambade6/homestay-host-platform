package com.homestay.platform.web;

import com.homestay.platform.model.Property;
import com.homestay.platform.service.PropertyService;
import com.homestay.platform.web.dto.PropertyForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Property listing for everyone; create / edit restricted to HOST and ADMIN in SecurityConfig. */
@Controller
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("properties", propertyService.findAll());
        return "properties/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new PropertyForm());
        model.addAttribute("propertyId", null);
        return "properties/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") PropertyForm form, BindingResult result,
                         Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("propertyId", null);
            return "properties/form";
        }
        Property p = propertyService.create(form);
        flash.addFlashAttribute("success", "Property " + p.getName() + " added");
        return "redirect:/properties";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", PropertyForm.from(propertyService.get(id)));
        model.addAttribute("propertyId", id);
        return "properties/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") PropertyForm form,
                         BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("propertyId", id);
            return "properties/form";
        }
        Property p = propertyService.update(id, form);
        flash.addFlashAttribute("success", "Property " + p.getName() + " updated");
        return "redirect:/properties";
    }
}
