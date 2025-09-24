package fiap.com.br.autottu.domain.service;

package fiap.com.br.autottu.security;

import fiap.com.br.autottu.domain.model.Usuario;
import fiap.com.br.autottu.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com email: " + email));

        var authorities = usuario.getFuncoes().stream()
                .map(funcao -> {
                    String nome = funcao.getNome().toString();
                    return nome.startsWith("ROLE_") ?
                            new SimpleGrantedAuthority(nome) :
                            new SimpleGrantedAuthority("ROLE_" + nome);
                })
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),      // e-mail como username
                usuario.getSenha(),      // senha já criptografada (BCrypt)
                usuario.isAtivo(),       // enabled
                true,                    // accountNonExpired
                true,                    // credentialsNonExpired
                true,                    // accountNonLocked
                authorities
        );
    }
}
