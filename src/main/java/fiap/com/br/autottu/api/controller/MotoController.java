import fiap.com.br.autottu.application.facade.MotoFacade;
import fiap.com.br.autottu.api.dto.MotoDTO;
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

    private final MotoFacade motoFacade;

    @GetMapping
    public String list(@RequestParam(name = "page", required = false) Integer page,
                       @RequestParam(name = "size", required = false) Integer size,
                       Model model) {
        var p = motoFacade.listarPaginado(page, size);

        model.addAttribute("pageTitle", "Motos");
        model.addAttribute("motos", p.getContent());
        model.addAttribute("page", p.getNumber());
        model.addAttribute("size", p.getSize());
        model.addAttribute("totalPages", p.getTotalPages());
        model.addAttribute("totalElements", p.getTotalElements());
        model.addAttribute("hasNext", p.hasNext());
        model.addAttribute("hasPrevious", p.hasPrevious());
        return "moto/list";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("pageTitle", "Nova Moto");
        model.addAttribute("moto", new MotoDTO());
        return "moto/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("pageTitle", "Editar Moto");
        model.addAttribute("moto", motoFacade.buscarPorIdDTO(id));
        return "moto/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("moto") MotoDTO dto,
                        BindingResult binding,
                        RedirectAttributes ra,
                        Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("pageTitle", "Nova Moto");
            return "moto/form";
        }
        motoFacade.criar(dto);
        ra.addFlashAttribute("msgSucesso", "Moto criada com sucesso!");
        return "redirect:/motos";
    }

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
            motoFacade.atualizar(id, dto);
            ra.addFlashAttribute("msgSucesso", "Moto atualizada!");
        } else if ("delete".equalsIgnoreCase(method)) {
            motoFacade.excluir(id);
            ra.addFlashAttribute("msgSucesso", "Moto excluída!");
        }
        return "redirect:/motos";
    }
}