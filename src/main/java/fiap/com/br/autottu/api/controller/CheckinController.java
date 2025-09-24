package fiap.com.br.autottu.api.controller;

import fiap.com.br.autottu.application.facade.CheckinFacade;
import fiap.com.br.autottu.api.request.CheckinRequest;
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

    private final CheckinFacade checkinFacade;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("pageTitle", "Check-ins");
        model.addAttribute("checkins", checkinFacade.listarTodos());
        return "checkin/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("pageTitle", "Novo Check-in");
        model.addAttribute("checkin", new CheckinRequest(null, null, null, false, null, null));
        model.addAttribute("slotsLivres", checkinFacade.listarSlotsDisponiveis());
        model.addAttribute("motosElegiveis", checkinFacade.listarMotosElegiveis());
        model.addAttribute("usuarios", checkinFacade.listarUsuariosElegiveis()); // caso use seleção de atendente
        return "checkin/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("pageTitle", "Editar Check-in");
        model.addAttribute("checkin", checkinFacade.carregarParaEdicao(id));
        model.addAttribute("slotsLivres", checkinFacade.listarSlotsDisponiveis());
        model.addAttribute("motosElegiveis", checkinFacade.listarMotosElegiveis());
        model.addAttribute("usuarios", checkinFacade.listarUsuariosElegiveis());
        return "checkin/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("checkin") CheckinRequest req,
                        BindingResult binding,
                        RedirectAttributes ra,
                        Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("pageTitle", "Novo Check-in");
            model.addAttribute("slotsLivres", checkinFacade.listarSlotsDisponiveis());
            model.addAttribute("motosElegiveis", checkinFacade.listarMotosElegiveis());
            model.addAttribute("usuarios", checkinFacade.listarUsuariosElegiveis());
            return "checkin/form";
        }
        var dto = checkinFacade.realizarCheckin(req);
        ra.addFlashAttribute("msgSucesso", "Check-in realizado! ID: " + dto.id());
        return "redirect:/checkins";
    }

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
                model.addAttribute("slotsLivres", checkinFacade.listarSlotsDisponiveis());
                model.addAttribute("motosElegiveis", checkinFacade.listarMotosElegiveis());
                model.addAttribute("usuarios", checkinFacade.listarUsuariosElegiveis());
                return "checkin/form";
            }
            checkinFacade.atualizarCheckin(id, req);
            ra.addFlashAttribute("msgSucesso", "Check-in atualizado!");
        } else if ("delete".equalsIgnoreCase(method)) {
            checkinFacade.excluir(id);
            ra.addFlashAttribute("msgSucesso", "Check-in excluído!");
        }
        return "redirect:/checkins";
    }
}