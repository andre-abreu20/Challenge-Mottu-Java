package fiap.com.br.autottu.domain.service;


import fiap.com.br.autottu.api.dto.MotoDTO;
import fiap.com.br.autottu.api.mapper.MotoMapper;
import fiap.com.br.autottu.domain.model.Moto;
import fiap.com.br.autottu.domain.repository.MotoRepository;
import fiap.com.br.autottu.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class MotoService {

    private final MotoRepository repository;
    private final MotoMapper mapper;

    // Valores da Páginação default:
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 50;
    public static final int MAX_PAGE_SIZE = 100;

    /** Normaliza página/tamanho aplicando defaults e teto máximo */
    private PageRequest resolvePageRequest(Integer pagina, Integer itens) {
        int p = (pagina == null || pagina < 0) ? DEFAULT_PAGE : pagina;
        int s = (itens == null || itens <= 0) ? DEFAULT_SIZE : Math.min(itens, MAX_PAGE_SIZE);
        return PageRequest.of(p, s);
    }

    @Cacheable(value = "moto", key= "#id")
    public MotoDTO buscarPorId(Integer id) {
        Moto entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Moto não encontrada (id = " + id + ")"));
        return mapper.toDTO(entity);
    }

    @Cacheable(value = "motos",
            key = "'page:' + T(java.util.Objects).toString(#pagina) + ':size:' + T(java.util.Objects).toString(#itens)")
    public Page<MotoDTO> listarTodas(Integer pagina, Integer itens) {
        var pr = resolvePageRequest(pagina, itens);
        return repository.findAll(pr).map(mapper::toDTO);
    }
    @CacheEvict(value = "motos", allEntries = true)
    @Transactional
    public MotoDTO criar(MotoDTO dto) {
        Moto entity = mapper.toEntity(dto);
        Moto salvo = repository.save(entity);
        return mapper.toDTO(salvo);
    }
    @CacheEvict(value = "motos", allEntries = true)
    @Transactional
    public MotoDTO atualizar(Integer id, MotoDTO dto) {
        Moto atual = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Moto não encontrada para atualização"));

        mapper.updateEntityFromDto(dto, atual);

        return mapper.toDTO(repository.save(atual));
    }
    @CacheEvict(value = "motos", allEntries = true)
    @Transactional
    public void excluir(Integer id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Moto não encontrada para exclusão");
        }
        repository.deleteById(id);
    }
}
