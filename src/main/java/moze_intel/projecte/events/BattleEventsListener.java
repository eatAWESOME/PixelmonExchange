package moze_intel.projecte.events;

import java.math.BigInteger;
import java.util.ArrayList;
import com.pixelmonmod.pixelmon.api.events.ExperienceGainEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.Pixelmon;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;

public class BattleEventsListener {

	public BattleEventsListener() {
		Pixelmon.EVENT_BUS.addListener(this::onExperienceGain);
    }
	
	//Fails when lvl:100
	//Duplicated by Exp. All
	public void onExperienceGain(ExperienceGainEvent event) {
		if (event.isFromBattle()) {
			ServerPlayerEntity player = event.pokemon.getPlayerOwner();
			if (!(player == null)) {
				BattleController battleController = event.getBattleController();
				ArrayList<PixelmonWrapper> activePokemon = battleController.getActivePokemon();
				for (PixelmonWrapper activePixelmonWrapper : activePokemon) {
					if (event.pokemon.getPokemon().equals(activePixelmonWrapper.pokemon)) {
						ArrayList<PixelmonWrapper> activeFaintedPokemon = battleController.getActiveFaintedPokemon();
						for (PixelmonWrapper activeFaintedPixelmonWrapper : activeFaintedPokemon) {
			                Pokemon pokemon = activeFaintedPixelmonWrapper.pokemon;
			                if (pokemon.getOwnerPlayer() != player) {
			                	int cp = calculateCP(pokemon);
			                	IKnowledgeProvider knowledgeProvider = getKnowledgeProvider(player);
			                    BigInteger currentEmc = knowledgeProvider.getEmc();
			                    BigInteger newEmc = currentEmc.add(BigInteger.valueOf(cp * 5));
			                    knowledgeProvider.setEmc(newEmc);
			                    knowledgeProvider.syncEmc(player);
			                    TextComponent message = new StringTextComponent("Earned " + String.valueOf(cp * 5) + " EMC for defeating a level " + String.valueOf(pokemon.getPokemonLevel()) + " (" + String.valueOf(cp) + " CP) " + pokemon.getSpecies().getTranslatedName().getString());
			                    player.sendMessage(message, player.getUUID());
			                }
			            }
					}
				}
			}
		}
    }
	
	private int calculateCP(Pokemon pokemon) {
        double hp = pokemon.getStats().getHP();
        double attack = pokemon.getStats().getAttack();
        double defense = pokemon.getStats().getDefense();
        double specialAttack = pokemon.getStats().getSpecialAttack();
        double specialDefense = pokemon.getStats().getSpecialDefense();
        double speed = pokemon.getStats().getSpeed();
        double level = pokemon.getPokemonLevel();

        double cp = ((hp + attack + defense + specialAttack + specialDefense + speed) * level * 6) / 100;

        return (int) Math.floor(cp);
    }
	
	public static void addEmcToPlayer(ServerPlayerEntity player, long emcToAdd) {
        
    }
	
	public static IKnowledgeProvider getKnowledgeProvider(PlayerEntity player) {
	    // Get the capability and ensure it's the correct type
	    return player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY)
	                 .map(cap -> {
	                     if (cap instanceof IKnowledgeProvider) {
	                         return (IKnowledgeProvider) cap;
	                     } else {
	                         throw new IllegalArgumentException("Invalid capability type for transmutation knowledge");
	                     }
	                 })
	                 .orElseThrow(() -> new IllegalArgumentException("Player does not have transmutation knowledge capability"));
	}
}