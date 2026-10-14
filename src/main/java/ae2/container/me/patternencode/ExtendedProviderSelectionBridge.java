package ae2.container.me.patternencode;

import ae2.api.networking.IGrid;
import ae2.container.me.patternencode.PatternProviderUploadService.ProcessingPatternUploadPreparation;
import ae2.container.me.patternencode.PatternProviderUploadService.ProcessingPatternUploadResult;
import ae2.container.me.patternencode.ProviderSelectionSession.ProviderEntryAction;
import ae2.container.me.patternencode.ProviderSelectionSession.ProviderMappingAction;
import ae2.container.me.patternencode.ProviderSelectionSession.ProviderSelectionOverlayOpenRequest;
import ae2.core.worlddata.PatternProviderMappingData;
import ae2.me.service.ActivePatternProviderDirectory.ProviderKey;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class ExtendedProviderSelectionBridge {
    private final ProviderSelectionSession session;

    private ExtendedProviderSelectionBridge(Host host) {
        this.session = new ProviderSelectionSession(new ProviderSelectionSession.Host() {
            @Override
            public boolean isLinkConnected() {
                return host.isLinkConnected();
            }

            @Override
            public @Nullable IGrid getProviderSelectionGrid() {
                return host.getProviderSelectionGrid();
            }

            @Override
            public @Nullable IGrid requireProviderSelectionGrid() {
                return host.requireProviderSelectionGrid();
            }

            @Override
            public PatternProviderMappingData getProviderMappingData() {
                return host.getProviderMappingData();
            }

            @Override
            public EntityPlayer getPlayer() {
                return host.getPlayer();
            }

            @Override
            public World getWorld() {
                return host.getWorld();
            }

            @Override
            public int getWindowId() {
                return host.getWindowId();
            }

            @Override
            public ItemStack getEncodedPattern() {
                return host.getEncodedPattern();
            }

            @Override
            public void setEncodedPattern(ItemStack encodedPattern) {
                host.setEncodedPattern(encodedPattern);
            }

            @Override
            public ProcessingPatternUploadResult uploadProcessingPattern(ItemStack encodedPattern, IGrid grid,
                                                                        ProviderKey target) {
                return host.uploadProcessingPattern(encodedPattern, grid, target);
            }

            @Override
            public ProcessingPatternUploadPreparation prepareProcessingPatternUpload(ItemStack encodedPattern,
                                                                                    IGrid grid,
                                                                                    ProviderKey target) {
                return host.prepareProcessingPatternUpload(encodedPattern, grid, target);
            }

            @Override
            public void sendProviderDirectoryPage(ProviderDirectoryPage page) {
                host.sendProviderDirectoryPage(page);
            }

            @Override
            public void sendProviderMappingPage(ProviderMappingPage page) {
                host.sendProviderMappingPage(page);
            }

            @Override
            public void syncProviderDirectoryRevision(long revision) {
                host.syncProviderDirectoryRevision(revision);
            }

            @Override
            public void syncProviderSelectionOverlay(ProviderSelectionOverlayOpenRequest request) {
                host.syncProviderSelectionOverlay(request.nonce(), request.searchText(), request.mappingText());
            }
        });
    }

    public static ExtendedProviderSelectionBridge create(Host host) {
        return new ExtendedProviderSelectionBridge(host);
    }

    public interface Host {
        boolean isLinkConnected();

        @Nullable
        IGrid getProviderSelectionGrid();

        @Nullable
        IGrid requireProviderSelectionGrid();

        PatternProviderMappingData getProviderMappingData();

        EntityPlayer getPlayer();

        World getWorld();

        int getWindowId();

        ItemStack getEncodedPattern();

        void setEncodedPattern(ItemStack encodedPattern);

        ProcessingPatternUploadResult uploadProcessingPattern(ItemStack encodedPattern, IGrid grid,
                                                             ProviderKey target);

        ProcessingPatternUploadPreparation prepareProcessingPatternUpload(ItemStack encodedPattern, IGrid grid,
                                                                         ProviderKey target);

        void sendProviderDirectoryPage(ProviderDirectoryPage page);

        void sendProviderMappingPage(ProviderMappingPage page);

        void syncProviderDirectoryRevision(long revision);

        void syncProviderSelectionOverlay(long nonce, String searchText, String mappingText);
    }

    public void onBroadcastChanges() {
        this.session.onBroadcastChanges();
    }

    public long getDirectoryRevision() {
        return this.session.getDirectoryRevision();
    }

    public void open(@Nullable String searchText, @Nullable String mappingText) {
        this.session.open(searchText, mappingText);
    }

    public void close() {
        this.session.close();
    }

    public void requestProviderDirectoryPage(@Nullable ProviderDirectoryPageRequest request) {
        this.session.requestProviderDirectoryPage(request);
    }

    public void requestProviderMappingPage(@Nullable ProviderMappingPageRequest request) {
        this.session.requestProviderMappingPage(request);
    }

    public void uploadProcessingPatternToProvider(@Nullable ProviderEntryAction action) {
        this.session.uploadProcessingPatternToProvider(action);
    }

    public void bindProviderMapping(@Nullable ProviderMappingAction action) {
        this.session.bindProviderMapping(action);
    }

    public void bindAndUploadProcessingPatternToProvider(@Nullable ProviderMappingAction action) {
        this.session.bindAndUploadProcessingPatternToProvider(action);
    }

    public void unbindProviderMapping(@Nullable ProviderMappingAction action) {
        this.session.unbindProviderMapping(action);
    }

    public void rebuildMappingsFromActiveProviders() {
        this.session.rebuildMappingsFromActiveProviders();
    }
}
