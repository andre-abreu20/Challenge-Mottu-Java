package fiap.com.br.autottu.domain.repository;

import fiap.com.br.autottu.domain.model.Slot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlotRepository extends JpaRepository<Slot,Integer> {
    List<Slot> findByOcupadoFalse();
}
