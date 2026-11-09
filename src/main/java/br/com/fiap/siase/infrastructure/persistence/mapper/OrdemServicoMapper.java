package br.com.fiap.siase.infrastructure.persistence.mapper;

import br.com.fiap.siase.domain.model.ItemPeca;
import br.com.fiap.siase.domain.model.ItemServico;
import br.com.fiap.siase.domain.model.OrdemDeServico;
import br.com.fiap.siase.infrastructure.persistence.entity.ItemPecaEntity;
import br.com.fiap.siase.infrastructure.persistence.entity.ItemServicoEntity;
import br.com.fiap.siase.infrastructure.persistence.entity.OrdemServicoEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrdemServicoMapper {

    @Mapping(target = "itensServico", source = "itensServico")
    @Mapping(target = "itensPeca", source = "itensPeca")
    OrdemDeServico toDomain(OrdemServicoEntity entity);

    @Mapping(target = "itensServico", source = "itensServico")
    @Mapping(target = "itensPeca", source = "itensPeca")
    OrdemServicoEntity toEntity(OrdemDeServico domain);

    @AfterMapping
    default void linkChildren(@MappingTarget OrdemServicoEntity entity) {
        if (entity.getItensServico() != null)
            entity.getItensServico().forEach(i -> i.setOrdemServico(entity));
        if (entity.getItensPeca() != null)
            entity.getItensPeca().forEach(i -> i.setOrdemServico(entity));
    }

    ItemServico toDomain(ItemServicoEntity entity);
    ItemServicoEntity toEntity(ItemServico domain);

    ItemPeca toDomain(ItemPecaEntity entity);
    ItemPecaEntity toEntity(ItemPeca domain);

    List<ItemServico> toItemServicoDomainList(List<ItemServicoEntity> entities);
    List<ItemPeca> toItemPecaDomainList(List<ItemPecaEntity> entities);
}
