package fiap.com.br.autottu.api.controller;

import fiap.com.br.autottu.api.dto.CheckinRequest;
import fiap.com.br.autottu.domain.model.Checkin;
import fiap.com.br.autottu.domain.service.CheckinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/checkins")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    // LISTA: GET /checkins -> "checkin/list"
    @GetMapping
    public String list(Model model) {
        model.addAttribute("pageTitle", "Check-ins");
        model.addAttribute("checkins", checkinService.listarTodos());
        return "checkin/list";
    }

    // NOVO: GET /checkins/novo -> "checkin/form"
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("pageTitle", "Novo Check-in");
        model.addAttribute("checkin", new CheckinRequest());
        model.addAttribute("slotsLivres", checkinService.listarSlotsDisponiveis()); // opcional
        model.addAttribute("motos", checkinService.listarMotosElegiveis()); // opcional
        return "checkin/form";
    }

    // EDITAR (se aplicável): GET /checkins/{id}/editar
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("pageTitle", "Editar Check-in");
        CheckinRequest req = checkinService.carregarParaEdicao(id); // mapeie para seu request
        model.addAttribute("checkin", req);
        model.addAttribute("slotsLivres", checkinService.listarSlotsDisponiveis());
        model.addAttribute("motos", checkinService.listarMotosElegiveis());
        return "checkin/form";
    }

    // CRIAR: POST /checkins (form)
    @PostMapping
    public String criar(@Valid @ModelAttribute("checkin") CheckinRequest req,
                        BindingResult binding,
                        RedirectAttributes ra,
                        Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("pageTitle", "Novo Check-in");
            model.addAttribute("slotsLivres", checkinService.listarSlotsDisponiveis());
            model.addAttribute("motos", checkinService.listarMotosElegiveis());
            return "checkin/form";
        }
        Checkin criado = checkinService.realizarCheckin(req);
        ra.addFlashAttribute("msgSucesso", "Check-in realizado com sucesso! ID: " + criado.getId());
        return "redirect:/checkins";
    }

    // ATUALIZAR/EXCLUIR: POST /checkins/{id} com _method
    @PostMapping("/{id}")
    public String atualizarOuExcluir(@PathVariable Integer id,
                                     @RequestParam(value = "_method", required = false) String method,
                                     @Valid @ModelAttribute("checkin") CheckinRequest req,
                                     BindingResult binding,
                                     RedirectAttributes ra,
                                     Model model) {
        if ("put".equalsIgnoreCase(method)) {
            if (binding.hasErrors()) {
                model.addAttribute("pageTitle", "Editar Check-in");
                model.addAttribute("slotsLivres", checkinService.listarSlotsDisponiveis());
                model.addAttribute("motos", checkinService.listarMotosElegiveis());
                return "checkin/form";
            }
            checkinService.atualizarCheckin(id, req);
            ra.addFlashAttribute("msgSucesso", "Check-in atualizado!");
        } else if ("delete".equalsIgnoreCase(method)) {
            checkinService.excluir(id);
            ra.addFlashAttribute("msgSucesso", "Check-in excluído!");
        }
        return "redirect:/checkins";
    }

    // alternativa explícita: POST /checkins/{id}/delete
    @PostMapping("/{id}/delete")
    public String excluir(@PathVariable Integer id, RedirectAttributes ra) {
        checkinService.excluir(id);
        ra.addFlashAttribute("msgSucesso", "Check-in excluído!");
        return "redirect:/checkins";
    }
}
