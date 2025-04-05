package moze_intel.projecte.events;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import com.pixelmonmod.pixelmon.api.events.ApricornEvent;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent.SuccessfulCapture;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent.SuccessfulRaidCapture;
import com.pixelmonmod.pixelmon.api.events.DropEvent;
import com.pixelmonmod.pixelmon.api.events.ExperienceGainEvent;
import com.pixelmonmod.pixelmon.api.events.PickupEvent;
import com.pixelmonmod.pixelmon.api.events.PokeLootEvent;
import com.pixelmonmod.pixelmon.api.events.PokeStopEvent;
import com.pixelmonmod.pixelmon.api.events.ShopkeeperEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.entities.pixelmon.drops.DroppedItem;
import com.pixelmonmod.pixelmon.Pixelmon;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.utils.EMCHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;

public class PixelmonListener {

	public PixelmonListener() {
		Pixelmon.EVENT_BUS.addListener(this::onExperienceGain);
		Pixelmon.EVENT_BUS.addListener(this::onCapture);
		Pixelmon.EVENT_BUS.addListener(this::onRaidCapture);
		Pixelmon.EVENT_BUS.addListener(this::onApricornPick);
		Pixelmon.EVENT_BUS.addListener(this::onDrop);
		Pixelmon.EVENT_BUS.addListener(this::onPickup);
		Pixelmon.EVENT_BUS.addListener(this::onPokeLoot);
		Pixelmon.EVENT_BUS.addListener(this::onPokeStop);
		Pixelmon.EVENT_BUS.addListener(this::onShopkeeperPurchase);
    }
	
	//Fails when lvl:100
	//Doesn't include Exp from catching
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
			                    BigInteger newEmc = currentEmc.add(BigInteger.valueOf(cp));
			                    knowledgeProvider.setEmc(newEmc);
			                    knowledgeProvider.syncEmc(player);
			                    TextComponent message = new StringTextComponent("Earned " + String.valueOf(cp) + " EMC for defeating a level " + String.valueOf(pokemon.getPokemonLevel()) + " (" + String.valueOf(cp) + " CP) " + pokemon.getSpecies().getTranslatedName().getString());
			                    player.sendMessage(message, player.getUUID());
			                }
			            }
					}
				}
			}
		}
    }
	
	public void onCapture(SuccessfulCapture event) {
		ServerPlayerEntity player = event.getPlayer();
		Pokemon pokemon = event.getPokemon().getPokemon();
    	int cp = calculateCP(pokemon);
    	IKnowledgeProvider knowledgeProvider = getKnowledgeProvider(player);
        BigInteger currentEmc = knowledgeProvider.getEmc();
        BigInteger newEmc = currentEmc.add(BigInteger.valueOf(cp * 5));
        knowledgeProvider.setEmc(newEmc);
        knowledgeProvider.syncEmc(player);
        TextComponent message = new StringTextComponent("Earned " + String.valueOf(cp * 5) + " EMC for catching a level " + String.valueOf(pokemon.getPokemonLevel()) + " (" + String.valueOf(cp) + " CP) " + pokemon.getSpecies().getTranslatedName().getString());
        player.sendMessage(message, player.getUUID());
    }
	
	public void onRaidCapture(SuccessfulRaidCapture event) {
		ServerPlayerEntity player = event.getPlayer();
		Pokemon pokemon = event.getRaidPokemon();
    	int cp = calculateCP(pokemon);
    	IKnowledgeProvider knowledgeProvider = getKnowledgeProvider(player);
        BigInteger currentEmc = knowledgeProvider.getEmc();
        BigInteger newEmc = currentEmc.add(BigInteger.valueOf(cp * 10));
        knowledgeProvider.setEmc(newEmc);
        knowledgeProvider.syncEmc(player);
        TextComponent message = new StringTextComponent("Earned " + String.valueOf(cp * 10) + " EMC for catching a level " + String.valueOf(pokemon.getPokemonLevel()) + " (" + String.valueOf(cp) + " CP) " + pokemon.getSpecies().getTranslatedName().getString());
        player.sendMessage(message, player.getUUID());
    }
	
	public void onApricornPick(ApricornEvent.Pick event) {
		ServerPlayerEntity player = event.getPlayer();
		ItemStack itemStack = event.getPickedStack();
		knowledgeCheck(player, itemStack);
	}
	
	public void onDrop(DropEvent event) {
		ServerPlayerEntity player = event.player;
		for (DroppedItem droppedItem : event.getDrops()) {
			ItemStack itemStack = droppedItem.item;
			knowledgeCheck(player, itemStack);
	    }
	}
	
	public void onPickup(PickupEvent event) {
		ServerPlayerEntity player = event.player.player;
		ItemStack itemStack = event.stack;
		knowledgeCheck(player, itemStack);
	}
	
	public void onPokeLoot(PokeLootEvent.GetDrops event) {
		ServerPlayerEntity player = event.player;
		ItemStack[] itemStacks = event.getDrops();
		for (ItemStack itemStack : itemStacks) {
			knowledgeCheck(player, itemStack);
		}
	}
	
	public void onPokeStop(PokeStopEvent.Drops event) {
		PlayerEntity player = event.getPlayer();
		List<ItemStack> itemStacks = event.getDrops();
		for (ItemStack itemStack : itemStacks) {
			knowledgeCheck(player, itemStack);
		}
	}
	
	public void onShopkeeperPurchase(ShopkeeperEvent.Purchase event) {
		ServerPlayerEntity player = event.getEntityPlayer();
		ItemStack itemStack = event.getItem();
		knowledgeCheck(player, itemStack);
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

	public static void knowledgeCheck(PlayerEntity player, ItemStack itemStack) {
		if (player instanceof ServerPlayerEntity) {
	        try {
	            IKnowledgeProvider knowledgeProvider = getKnowledgeProvider(player);
	            if (itemStack != null) {
	            	if (!itemStack.isEmpty() && EMCHelper.getEmcValue(itemStack) > 0) {
		                if (!knowledgeProvider.hasKnowledge(itemStack)) {
		                    knowledgeProvider.addKnowledge(itemStack);
		                    knowledgeProvider.syncKnowledgeChange((ServerPlayerEntity) player, ItemInfo.fromStack(itemStack), true);
		                    TextComponent message = new StringTextComponent("Learned " + itemStack.getItem().getName(itemStack).getString() + " Transmutation Knowledge");
		                    player.sendMessage(message, player.getUUID());
		                }
		            }
	            }
	        } catch (IllegalArgumentException e) {
	            System.err.println("Error retrieving knowledge provider: " + e.getMessage());
	        }
	    }
	}
	
	public static IKnowledgeProvider getKnowledgeProvider(PlayerEntity player) {
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