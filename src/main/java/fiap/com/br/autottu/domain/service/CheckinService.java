package fiap.com.br.autottu.domain.service;

import fiap.com.br.autottu.api.dto.CheckinRequest;
import fiap.com.br.autottu.api.mapper.CheckinMapper;
import fiap.com.br.autottu.domain.model.Checkin;
import fiap.com.br.autottu.domain.model.Moto;
import fiap.com.br.autottu.domain.model.Slot;
import fiap.com.br.autottu.domain.model.Usuario;
import fiap.com.br.autottu.domain.repository.CheckinRepository;
import fiap.com.br.autottu.domain.repository.MotoRepository;
import fiap.com.br.autottu.domain.repository.SlotRepository;
import fiap.com.br.autottu.domain.repository.UsuarioRepository;
import fiap.com.br.autottu.shared.exception.BusinessException;
import fiap.com.br.autottu.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckinService {

    private final CheckinRepository checkinRepo;
    private final MotoRepository motoRepo;
    private final SlotRepository slotRepo;
    private final UsuarioRepository usuarioRepo;
    private final CheckinMapper mapper;

    /* ==========================
       Listagens para as telas
       ========================== */

    public List<Checkin> listarTodos() {
        return checkinRepo.findAll();
    }

    /** Slots livres para o <select> do formulário de check-in */
    public List<Slot> listarSlotsDisponiveis() {
        // requer o método findByOcupadoFalse() no SlotRepository (abaixo)
        return slotRepo.findByOcupadoFalse();
    }

    /** Motos elegíveis (sem slot associado) para o <select> do formulário */
    public List<Moto> listarMotosElegiveis() {
        // requer o método findBySlotIsNull() no MotoRepository (abaixo)
        return motoRepo.findBySlotIsNull();
    }

    /* ==========================
       Carga de edição e detalhes
       ========================== */

    /** Carrega um Checkin e converte para o DTO usado no form (CheckinRequest) */
    public CheckinRequest carregarParaEdicao(Integer id) {
        Checkin c = getCheckinOrThrow(id);
        return mapper.toDTO(c);
    }

    /* ==========================
       CRUD de Check-in (MVC)
       ========================== */

    /** Criação do check-in com regras de negócio básicas */
    @Transactional
    public CheckinRequest realizarCheckin(CheckinRequest req) {
        Moto moto = getMotoOrThrow(req.motoId());
        Slot slot = getSlotOrThrow(req.slotId());
        Usuario usuario = getUsuarioOrThrow(req.usuarioId());

        // Regra: slot deve estar livre
        if (Boolean.TRUE.equals(slot.getOcupado())) {
            throw new BusinessException("Slot " + slot.getId() + " já está ocupado.");
        }
        // Regra: a moto não pode estar ocupando outro slot
        if (moto.getSlot() != null) {
            throw new BusinessException("A moto " + moto.getId() + " já está alocada no slot " + moto.getSlot().getId());
        }

        // Monta entidade Checkin a partir do request
        Checkin entity = new Checkin();
        entity.setObservacao(req.observacao());
        entity.setImagens(req.imagens());
        entity.setViolada(Boolean.TRUE.equals(req.violada()) ? "Y" : "N"); // ajuste conforme seu converter/campo
        entity.setMoto(moto);
        entity.setUsuario(usuario);

        Checkin salvo = checkinRepo.save(entity);

        // Atualiza o Slot e amarra na Moto
        slot.setMoto(moto);
        slot.setOcupado(true);
        slotRepo.save(slot);

        moto.setSlot(slot); // mantém o vínculo dos dois lados (se seu mapeamento for bidirecional)
        motoRepo.save(moto);

        return mapper.toDTO(salvo);
    }

    /** Atualização do check-in (permite trocar slot/observação/imagens/violada) */
    @Transactional
    public CheckinRequest atualizarCheckin(Integer id, CheckinRequest req) {
        Checkin atual = getCheckinOrThrow(id);

        // Atualiza campos simples
        atual.setObservacao(req.observacao());
        atual.setImagens(req.imagens());
        atual.setViolada(Boolean.TRUE.equals(req.violada()) ? "Y" : "N");

        // Troca de MOTO (opcional — só se veio diferente)
        if (req.motoId() != null && (atual.getMoto() == null || !req.motoId().equals(atual.getMoto().getId()))) {
            Moto novaMoto = getMotoOrThrow(req.motoId());
            // Se a nova moto já tem slot -> erro
            if (novaMoto.getSlot() != null) {
                throw new BusinessException("A moto " + novaMoto.getId() + " já está alocada no slot " + novaMoto.getSlot().getId());
            }
            // Se existia moto anterior, desamarra do slot dela
            if (atual.getMoto() != null && atual.getMoto().getSlot() != null) {
                Slot antigo = atual.getMoto().getSlot();
                antigo.setMoto(null);
                antigo.setOcupado(false);
                slotRepo.save(antigo);
                atual.getMoto().setSlot(null);
                motoRepo.save(atual.getMoto());
            }
            atual.setMoto(novaMoto);
        }

        // Troca de SLOT (opcional — só se veio diferente)
        if (req.slotId() != null) {
            Slot slotDestino = getSlotOrThrow(req.slotId());
            if (Boolean.TRUE.equals(slotDestino.getOcupado())) {
                // se já está ocupado por outra moto, erro
                if (slotDestino.getMoto() == null || atual.getMoto() == null ||
                        !slotDestino.getMoto().getId().equals(atual.getMoto().getId())) {
                    throw new BusinessException("Slot " + slotDestino.getId() + " já está ocupado.");
                }
            }

            // libera slot atual (se houver e se for diferente)
            Moto motoAtual = atual.getMoto();
            if (motoAtual != null) {
                Slot slotAtual = motoAtual.getSlot();
                if (slotAtual != null && !slotAtual.getId().equals(slotDestino.getId())) {
                    slotAtual.setMoto(null);
                    slotAtual.setOcupado(false);
                    slotRepo.save(slotAtual);
                }
                // aloca novo slot
                slotDestino.setMoto(motoAtual);
                slotDestino.setOcupado(true);
                slotRepo.save(slotDestino);

                motoAtual.setSlot(slotDestino);
                motoRepo.save(motoAtual);
            }
        }

        // Troca de USUÁRIO (opcional)
        if (req.usuarioId() != null && (atual.getUsuario() == null || !req.usuarioId().equals(atual.getUsuario().getId()))) {
            Usuario novoUsuario = getUsuarioOrThrow(req.usuarioId());
            atual.setUsuario(novoUsuario);
        }

        Checkin salvo = checkinRepo.save(atual);
        return mapper.toDTO(salvo);
    }

    /** Exclusão do check-in liberando o slot da moto */
    @Transactional
    public void excluir(Integer id) {
        Checkin c = getCheckinOrThrow(id);

        Moto moto = c.getMoto();
        if (moto != null) {
            Slot slot = moto.getSlot();
            if (slot != null) {
                slot.setMoto(null);
                slot.setOcupado(false);
                slotRepo.save(slot);

                moto.setSlot(null);
                motoRepo.save(moto);
            }
        }
        checkinRepo.deleteById(id);
    }

    /* ==========================
       Helpers (buscas + erros)
       ========================== */

    private Checkin getCheckinOrThrow(Integer id) {
        return checkinRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Check-in não encontrado: id=" + id));
    }

    private Moto getMotoOrThrow(Integer id) {
        return motoRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Moto não encontrada: id=" + id));
    }

    private Slot getSlotOrThrow(Integer id) {
        return slotRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Slot não encontrado: id=" + id));
    }

    private Usuario getUsuarioOrThrow(Integer id) {
        return usuarioRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado: id=" + id));
    }
}
