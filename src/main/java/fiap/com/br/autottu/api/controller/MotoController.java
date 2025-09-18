package fiap.com.br.autottu.web;

import fiap.com.br.autottu.api.dto.MotoDTO;
import fiap.com.br.autottu.domain.service.MotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/motos")
@RequiredArgsConstructor
public class MotoController {

    private final MotoService motoService;

    // LISTA: GET /motos -> "moto/list"
    @GetMapping
    public String list(Model model) {
        model.addAttribute("pageTitle", "Motos");
        model.addAttribute("motos", motoService.listarTodas(30, 10));
        return "moto/list";
    }

    // NOVO: GET /motos/novo -> "moto/form"
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("pageTitle", "Nova Moto");
        model.addAttribute("moto", new MotoDTO());
        return "moto/form";
    }

    // EDITAR: GET /motos/{id}/editar -> "moto/form"
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("pageTitle", "Editar Moto");
        model.addAttribute("moto", motoService.buscarPorId(id));
        return "moto/form";
    }

    // CRIAR: POST /motos (form)
    @PostMapping
    public String criar(@Valid @ModelAttribute("moto") MotoDTO dto,
                        BindingResult binding,
                        RedirectAttributes ra,
                        Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("pageTitle", "Nova Moto");
            return "moto/form";
        }
        motoService.criar(dto);
        ra.addFlashAttribute("msgSucesso", "Moto criada com sucesso!");
        return "redirect:/motos";
    }

    // ATUALIZAR/EXCLUIR (via _method=put|delete): POST /motos/{id}
    @PostMapping("/{id}")
    public String atualizarOuExcluir(@PathVariable Integer id,
                                     @RequestParam(value = "_method", required = false) String method,
                                     @Valid @ModelAttribute("moto") MotoDTO dto,
                                     BindingResult binding,
                                     RedirectAttributes ra,
                                     Model model) {
        if ("put".equalsIgnoreCase(method)) {
            if (binding.hasErrors()) {
                model.addAttribute("pageTitle", "Editar Moto");
                return "moto/form";
            }
            motoService.atualizar(id, dto);
            ra.addFlashAttribute("msgSucesso", "Moto atualizada com sucesso!");
        } else if ("delete".equalsIgnoreCase(method)) {
            motoService.excluir(id);
            ra.addFlashAttribute("msgSucesso", "Moto excluída!");
        }
        return "redirect:/motos";
    }

    // alternativa explícita: POST /motos/{id}/delete
    @PostMapping("/{id}/delete")
    public String excluir(@PathVariable Integer id, RedirectAttributes ra) {
        motoService.excluir(id);
        ra.addFlashAttribute("msgSucesso", "Moto excluída!");
        return "redirect:/motos";
    }
}
