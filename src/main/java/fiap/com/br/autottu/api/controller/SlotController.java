package fiap.com.br.autottu.api.controller;

import fiap.com.br.autottu.domain.model.Slot;
import fiap.com.br.autottu.domain.service.SlotService;
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

    private final SlotService slotService;

    // LISTA: GET /slots -> "slot/list"
    @GetMapping
    public String list(Model model) {
        model.addAttribute("pageTitle", "Slots");
        model.addAttribute("slots", slotService.listarTodos());
        return "slot/list";
    }

    // NOVO: GET /slots/novo -> "slot/form"
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("pageTitle", "Novo Slot");
        model.addAttribute("slot", new Slot());
        return "slot/form";
    }

    // EDITAR: GET /slots/{id}/editar -> "slot/form"
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("pageTitle", "Editar Slot");
        model.addAttribute("slot", slotService.buscarPorId(id));
        return "slot/form";
    }

    // CRIAR: POST /slots
    @PostMapping
    public String criar(@Valid @ModelAttribute("slot") Slot slot,
                        BindingResult binding,
                        RedirectAttributes ra,
                        Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("pageTitle", "Novo Slot");
            return "slot/form";
        }
        slotService.criar(slot);
        ra.addFlashAttribute("msgSucesso", "Slot criado!");
        return "redirect:/slots";
    }

    // ATUALIZAR/EXCLUIR: POST /slots/{id} com _method
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
            slotService.atualizar(id, slot);
            ra.addFlashAttribute("msgSucesso", "Slot atualizado!");
        } else if ("delete".equalsIgnoreCase(method)) {
            slotService.excluir(id);
            ra.addFlashAttribute("msgSucesso", "Slot excluído!");
        }
        return "redirect:/slots";
    }

    // alternativa explícita: POST /slots/{id}/delete
    @PostMapping("/{id}/delete")
    public String excluir(@PathVariable Integer id, RedirectAttributes ra) {
        slotService.excluir(id);
        ra.addFlashAttribute("msgSucesso", "Slot excluído!");
        return "redirect:/slots";
    }
}
