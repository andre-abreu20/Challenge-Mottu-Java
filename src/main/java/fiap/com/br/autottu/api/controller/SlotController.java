package fiap.com.br.autottu.api.controller;

import fiap.com.br.autottu.application.facade.SlotFacade;
import fiap.com.br.autottu.domain.model.Slot;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/slots")
@RequiredArgsConstructor
public class SlotController {

    private final SlotFacade slotFacade;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("pageTitle", "Slots");
        model.addAttribute("slots", slotFacade.listarTodos());
        return "slot/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("pageTitle", "Novo Slot");
        model.addAttribute("slot", new Slot());
        return "slot/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("pageTitle", "Editar Slot");
        model.addAttribute("slot", slotFacade.buscarPorId(id));
        return "slot/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("slot") Slot slot,
                        BindingResult binding,
                        RedirectAttributes ra,
                        Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("pageTitle", "Novo Slot");
            return "slot/form";
        }
        slotFacade.criar(slot);
        ra.addFlashAttribute("msgSucesso", "Slot criado!");
        return "redirect:/slots";
    }

    @PostMapping("/{id}")
    public String atualizarOuExcluir(@PathVariable Integer id,
                                     @RequestParam(value = "_method", required = false) String method,
                                     @Valid @ModelAttribute("slot") Slot slot,
                                     BindingResult binding,
                                     RedirectAttributes ra,
                                     Model model) {
        if ("put".equalsIgnoreCase(method)) {
            if (binding.hasErrors()) {
                model.addAttribute("pageTitle", "Editar Slot");
                return "slot/form";
            }
            slotFacade.atualizar(id, slot);
            ra.addFlashAttribute("msgSucesso", "Slot atualizado!");
        } else if ("delete".equalsIgnoreCase(method)) {
            slotFacade.excluir(id);
            ra.addFlashAttribute("msgSucesso", "Slot excluído!");
        }
        return "redirect:/slots";
    }
}