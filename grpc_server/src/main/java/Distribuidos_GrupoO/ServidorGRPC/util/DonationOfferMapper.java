package Distribuidos_GrupoO.ServidorGRPC.util;

import Distribuidos_GrupoO.ServidorGRPC.model.InventarioDeDonaciones;
import Distribuidos_GrupoO.ServidorGRPC.model.Usuario;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.offer.DonationOffer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DonationOfferMapper {
    public static List<InventarioDeDonaciones> toInventarioList(DonationOffer offer, Usuario usuarioAlta) {
        List<InventarioDeDonaciones> list = new ArrayList<>();
        if (offer.getDonations() != null) {
            for (DonationOffer.DonationItem item : offer.getDonations()) {
                InventarioDeDonaciones inv = new InventarioDeDonaciones();
                inv.setCategoria(parseCategoria(item.getCategory()));
                inv.setDescripcion(item.getDescription());
                inv.setCantidad(parseCantidad(item.getQuantity()));
                inv.setEliminado(false);
                inv.setFechaAlta(LocalDateTime.now());
                inv.setUsuarioAlta(usuarioAlta);
                list.add(inv);
            }
        }
        return list;
    }

    public static DonationOffer fromInventarioList(List<InventarioDeDonaciones> inventarioList, String offerId, String organizationId) {
        List<DonationOffer.DonationItem> items = new ArrayList<>();
        for (InventarioDeDonaciones inv : inventarioList) {
            items.add(new DonationOffer.DonationItem(
                inv.getCategoria().name(),
                inv.getDescripcion(),
                String.valueOf(inv.getCantidad())
            ));
        }
        return new DonationOffer(offerId, organizationId, items);
    }

    private static InventarioDeDonaciones.CategoriaEnum parseCategoria(String categoria) {
        try {
            return InventarioDeDonaciones.CategoriaEnum.valueOf(categoria.toUpperCase());
        } catch (Exception e) {
            return InventarioDeDonaciones.CategoriaEnum.ALIMENTOS; // default
        }
    }

    private static Integer parseCantidad(String cantidad) {
        try {
            return Integer.parseInt(cantidad);
        } catch (Exception e) {
            return 1;
        }
    }
}
